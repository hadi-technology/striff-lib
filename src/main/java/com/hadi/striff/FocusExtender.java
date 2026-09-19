package com.hadi.striff;

import com.hadi.clarpse.sourcemodel.OOPSourceCodeModel;

import java.util.Set;

/**
 * Chooses more files to analyse in full, from the first compile of a one-level analysis.
 *
 * <p>A one-level analysis models the filter's files in full and the files they reference as
 * boundary components, whose own outgoing references are not followed. After that first compile,
 * a caller can look at what the change connects to and name files it needs modelled in full: their
 * outgoing references complete, and they no longer boundary. The analysis then adds them to the
 * analysed files of both revisions and compiles again; the diff, the relationships and the diagram
 * are built from that final compile only.
 *
 * <p>It is called once per operation, on the calling thread, with every language's first-pass
 * model merged per revision. The models are the first pass's own and must not be modified.
 */
@FunctionalInterface
public interface FocusExtender {

    /**
     * The files to add to the analysed files of both revisions.
     *
     * @param base the base revision's first-pass model
     * @param head the head revision's first-pass model
     * @return paths in the form of {@code ProjectFile.path()}; {@code null} or empty adds nothing
     */
    Set<String> morePaths(OOPSourceCodeModel base, OOPSourceCodeModel head);
}
