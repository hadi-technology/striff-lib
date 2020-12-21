# striff-lib
[![maintained-by](https://img.shields.io/badge/Maintained%20by-Hadii%20Technologies-violet.svg)](https://hadii.ca) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.hadii-tech/stiff-lib/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.hadii-tech/stiff-lib) [![Build Status](https://travis-ci.com/hadii-tech/stiff-lib.svg?branch=master)](https://travis-ci.com/hadii-tech/stiff-lib) [![Codacy Badge](https://app.codacy.com/project/badge/Grade/f52c429a0a514abf86d252fe263d7c17)](https://www.codacy.com/gh/hadii-tech/stiff-lib?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=hadii-tech/stiff-lib&amp;utm_campaign=Badge_Grade) [![codecov](https://codecov.io/gh/hadii-tech/stiff-lib/branch/master/graph/badge.svg)](https://codecov.io/gh/hadii-tech/stiff-lib) [![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT) [![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg?style=flat-square)](http://makeapullrequest.com)

A java library for generating striff (shortened from "structural diff") diagrams. 

From the [original paper](https://1drv.ms/b/s!AtNem4nrrzWGtbIhRmESfVtmfmpcQA) abstract:

"Despite recent advancements in automated code quality
and defect finding tools, developers spend a significant
amount of time completing code reviews. Code under-
standability is a key contributor to this phenomenon,
since engineers need to understand both microscopic
and macroscopic level details of the code under review.
Existing tools for code reviews including diffing, inline
commenting and syntax highlighting provide limited
support for the macroscopic understanding needs of
reviewers. When reviewing code for architectural and
design quality, such tools do not enable reviewers to
understand the code from a top-down lens which the
original architects of the code would have likely used to
design the system. To overcome these limitations and
to complement existing approaches, we introduce struc-
ture diff (striff) diagrams. Striffs provide reviewers with
an architectural understanding of the incoming code in
relation to the existing system, allowing reviewers to
gain a more complete view of the scope and impact of
the proposed code changes in a code review."

![sample_striff](striff.png)






