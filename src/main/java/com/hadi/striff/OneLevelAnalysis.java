package com.hadi.striff;

import com.hadi.clarpse.compiler.AnalysisOptions;
import com.hadi.clarpse.compiler.ClarpseProject;
import com.hadi.clarpse.compiler.CompileException;
import com.hadi.clarpse.compiler.CompileFailure;
import com.hadi.clarpse.compiler.CompileResult;
import com.hadi.clarpse.compiler.Lang;
import com.hadi.clarpse.compiler.PreparedAnalysis;
import com.hadi.clarpse.compiler.ProjectFile;
import com.hadi.clarpse.compiler.ProjectFiles;
import com.hadi.clarpse.sourcemodel.OOPSourceCodeModel;
import com.hadi.clarpse.sourcemodel.StringPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Compiles the base and head revisions of a one-level analysis into the two models a diff is built
 * from.
 *
 * <p>For each language with files in the filter, both revisions are prepared with the filter's files
 * as analysed files, and each is compiled with the same extra files: the union of the two
 * revisions' level-one files, so a type loaded in one revision is loaded in the other, plus the
 * context files. With a {@link FocusExtender}, the first compile's models are handed to it once, the
 * files it names are added to the analysed files of both revisions, and both are compiled again. The
 * models returned are always those of the last compile.
 *
 * <p>Budgets: level one is capped by {@link StriffConfig#levelOneBudget()}, context files by
 * {@link StriffConfig#contextBudget()}, per language. Context files never displace level-one files:
 * when the level-one union reaches its budget, no context file is modelled, and level one may then
 * use the room the context budget leaves. When there are more context files than the budget allows,
 * those naming the analysed files most often are kept.
 *
 * <p>Every prepared analysis is closed before {@link #run} returns or throws, including on
 * interruption.
 */
final class OneLevelAnalysis {

    private static final Logger LOGGER = LoggerFactory.getLogger(OneLevelAnalysis.class);

    private OneLevelAnalysis() {
    }

    /**
     * The models of the final compile, the failures it reported, and what it modelled.
     *
     * @param base     the base revision's model
     * @param head     the head revision's model
     * @param failures compile failures of the final compile
     * @param scope    what was modelled beyond the filter's files
     */
    record Outcome(OOPSourceCodeModel base, OOPSourceCodeModel head, Set<CompileFailure> failures,
                   AnalysisScope scope) {
    }

    /** One language's prepared analyses, one per revision. */
    private static final class Prepared {
        private final Lang lang;
        private final PreparedAnalysis base;
        private final PreparedAnalysis head;

        Prepared(Lang lang, PreparedAnalysis base, PreparedAnalysis head) {
            this.lang = lang;
            this.base = base;
            this.head = head;
        }
    }

    /**
     * Runs the analysis.
     *
     * @param basePFs base revision files, every file of the repository
     * @param headPFs head revision files, every file of the repository
     * @param config  the configuration; {@link StriffConfig#oneLevel()} must hold
     * @return the final compile's models
     * @throws CompileException if a compile fails
     */
    static Outcome run(ProjectFiles basePFs, ProjectFiles headPFs, StriffConfig config)
            throws CompileException {
        final AnalysisOptions options = AnalysisOptions.oneLevel()
                .withLevelOneBudget(config.levelOneBudget() + config.contextBudget());
        final Set<String> focus = new LinkedHashSet<>(config.filesFilter());
        final List<Prepared> prepared = new ArrayList<>();
        try {
            for (Lang lang : config.languages()) {
                if (hasFileOf(lang, focus, basePFs, headPFs)) {
                    prepared.add(prepare(lang, focus, basePFs, headPFs, options));
                }
            }
            Outcome outcome = compileAll(prepared, focus, basePFs, headPFs, config, Set.of());
            final FocusExtender extender = config.focusExtender();
            if (extender == null) {
                return outcome;
            }
            final Set<String> more = new TreeSet<>();
            final Set<String> named = extender.morePaths(outcome.base(), outcome.head());
            if (named != null) {
                named.stream().filter(path -> path != null && !focus.contains(path)).forEach(more::add);
            }
            if (more.isEmpty()) {
                return outcome;
            }
            LOGGER.info("Adding {} file(s) named by the focus extender to the analysed files.", more.size());
            // The first compile's models are not used past this point; let them go before the second.
            outcome = null;
            extend(prepared, focus, more, basePFs, headPFs, config, options);
            final Set<String> extendedFocus = new LinkedHashSet<>(focus);
            extendedFocus.addAll(more);
            return compileAll(prepared, extendedFocus, basePFs, headPFs, config, more);
        } finally {
            prepared.forEach(OneLevelAnalysis::close);
        }
    }

    private static Prepared prepare(Lang lang, Collection<String> focus, ProjectFiles basePFs,
                                    ProjectFiles headPFs, AnalysisOptions options) throws CompileException {
        final ParallelParse.Results<PreparedAnalysis> both = ParallelParse.run(
                () -> new ClarpseProject(basePFs, lang, focus, options).prepare(),
                () -> new ClarpseProject(headPFs, lang, focus, options).prepare(),
                PreparedAnalysis::close);
        return new Prepared(lang, both.base(), both.head());
    }

    private static void extend(List<Prepared> prepared, Set<String> focus, Set<String> more,
                               ProjectFiles basePFs, ProjectFiles headPFs, StriffConfig config,
                               AnalysisOptions options) throws CompileException {
        final Set<Lang> preparedLangs = new HashSet<>();
        for (Prepared analyses : prepared) {
            preparedLangs.add(analyses.lang);
            if (!hasFileOf(analyses.lang, more, basePFs, headPFs)) {
                continue;
            }
            ParallelParse.run(
                    () -> {
                        analyses.base.extendFocus(more);
                        return Boolean.TRUE;
                    },
                    () -> {
                        analyses.head.extendFocus(more);
                        return Boolean.TRUE;
                    });
        }
        // A language the filter had no file of, but the extender named some of, starts here.
        final Set<String> all = new LinkedHashSet<>(focus);
        all.addAll(more);
        for (Lang lang : config.languages()) {
            if (!preparedLangs.contains(lang) && hasFileOf(lang, more, basePFs, headPFs)) {
                prepared.add(prepare(lang, all, basePFs, headPFs, options));
            }
        }
    }

    private static Outcome compileAll(List<Prepared> prepared, Set<String> focus, ProjectFiles basePFs,
                                      ProjectFiles headPFs, StriffConfig config, Set<String> extendedFocus)
            throws CompileException {
        // One pool across both revisions of one compile: the two models name almost entirely the
        // same packages, types and members.
        final StringPool names = new StringPool();
        final OOPSourceCodeModel baseModel = new OOPSourceCodeModel(names);
        final OOPSourceCodeModel headModel = new OOPSourceCodeModel(names);
        final Set<CompileFailure> failures = new HashSet<>();
        final Set<String> levelOne = new TreeSet<>();
        final Set<String> levelOneHeld = new TreeSet<>();
        final Set<String> context = new TreeSet<>();
        final Set<String> contextHeld = new TreeSet<>();
        for (Prepared analyses : prepared) {
            final Set<String> union = new TreeSet<>(analyses.base.levelOneFiles());
            union.addAll(analyses.head.levelOneFiles());
            final List<String> candidates = contextCandidates(analyses.lang, config.expandedFiles(), focus,
                    union, basePFs, headPFs);
            final List<String> kept;
            if (union.size() >= config.levelOneBudget()) {
                kept = List.of();
            } else {
                kept = rankContext(candidates, focus, basePFs, headPFs, config.contextBudget());
            }
            candidates.stream().filter(path -> !kept.contains(path)).forEach(contextHeld::add);
            context.addAll(kept);
            final Set<String> extra = new TreeSet<>(union);
            extra.addAll(kept);
            final ParallelParse.Results<CompileResult> results = ParallelParse.run(
                    () -> analyses.base.compile(extra),
                    () -> analyses.head.compile(extra));
            for (CompileResult result : List.of(results.base(), results.head())) {
                failures.addAll(result.failures());
                result.levelOne().levelOneFiles().stream()
                        .filter(path -> !kept.contains(path)).forEach(levelOne::add);
                result.levelOne().heldByBudget().stream()
                        .filter(path -> !kept.contains(path)).forEach(levelOneHeld::add);
            }
            LOGGER.info("One-level {}: {} level-one file(s), {} context file(s), base {} / head {} components.",
                    analyses.lang, union.size(), kept.size(), results.base().model().size(),
                    results.head().model().size());
            baseModel.merge(results.base().model());
            headModel.merge(results.head().model());
        }
        return new Outcome(baseModel, headModel, failures,
                new AnalysisScope(levelOne, levelOneHeld, context, contextHeld, extendedFocus));
    }

    /**
     * The context files of one language that are neither analysed nor already level one, in path
     * order.
     */
    private static List<String> contextCandidates(Lang lang, Set<String> contextFiles, Set<String> focus,
                                                  Set<String> levelOne, ProjectFiles basePFs,
                                                  ProjectFiles headPFs) {
        if (contextFiles.isEmpty()) {
            return new ArrayList<>();
        }
        final Set<String> ofLang = pathsOf(lang, basePFs);
        ofLang.addAll(pathsOf(lang, headPFs));
        final List<String> candidates = new ArrayList<>();
        new TreeSet<>(contextFiles).stream()
                .filter(ofLang::contains)
                .filter(path -> !focus.contains(path) && !levelOne.contains(path))
                .forEach(candidates::add);
        return candidates;
    }

    /**
     * The context files to model: all of them within the budget, otherwise those that name the
     * analysed files most often, ties broken by path.
     */
    private static List<String> rankContext(List<String> candidates, Set<String> focus, ProjectFiles basePFs,
                                            ProjectFiles headPFs, int budget) {
        if (candidates.size() <= budget) {
            return candidates;
        }
        final List<Pattern> names = new ArrayList<>();
        for (String path : focus) {
            final String name = baseName(path);
            if (!name.isEmpty()) {
                names.add(Pattern.compile("\\b" + Pattern.quote(name) + "\\b"));
            }
        }
        final Map<String, String> content = contentByPath(candidates, basePFs, headPFs);
        final Map<String, Integer> mentions = new HashMap<>();
        for (String path : candidates) {
            final String text = content.getOrDefault(path, "");
            int count = 0;
            for (Pattern name : names) {
                final Matcher matcher = name.matcher(text);
                while (matcher.find()) {
                    count++;
                }
            }
            mentions.put(path, count);
        }
        final List<String> ranked = new ArrayList<>(candidates);
        ranked.sort(Comparator.comparing((String path) -> -mentions.get(path)).thenComparing(path -> path));
        return new ArrayList<>(ranked.subList(0, budget));
    }

    private static Map<String, String> contentByPath(Collection<String> paths, ProjectFiles basePFs,
                                                     ProjectFiles headPFs) {
        final Set<String> wanted = new HashSet<>(paths);
        final Map<String, String> content = new HashMap<>();
        for (Collection<ProjectFile> revision : List.of(headPFs.files(), basePFs.files())) {
            for (ProjectFile file : revision) {
                if (wanted.contains(file.path()) && !content.containsKey(file.path()) && file.content() != null) {
                    content.put(file.path(), file.content());
                }
            }
        }
        return content;
    }

    private static String baseName(String path) {
        String name = path.replace('\\', '/');
        name = name.substring(name.lastIndexOf('/') + 1);
        final int dot = name.indexOf('.');
        if (dot > 0) {
            name = name.substring(0, dot);
        }
        return name;
    }

    private static boolean hasFileOf(Lang lang, Collection<String> paths, ProjectFiles basePFs,
                                     ProjectFiles headPFs) {
        final Set<String> ofLang = pathsOf(lang, basePFs);
        ofLang.addAll(pathsOf(lang, headPFs));
        return paths.stream().anyMatch(ofLang::contains);
    }

    private static Set<String> pathsOf(Lang lang, ProjectFiles files) {
        final Set<String> paths = new HashSet<>();
        for (ProjectFile file : files.files(lang)) {
            paths.add(file.path());
        }
        return paths;
    }

    private static void close(Prepared analyses) {
        closeQuietly(analyses.base);
        closeQuietly(analyses.head);
    }

    private static void closeQuietly(PreparedAnalysis analysis) {
        try {
            analysis.close();
        } catch (RuntimeException e) {
            LOGGER.warn("Could not close a prepared analysis.", e);
        }
    }
}
