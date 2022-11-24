# Architecture Decision Record: Handling Parse Errors

## Context
While generating striffs for a given code base, [clarpse](https://github.com/hadii-tech/clarpse) may
report compile failures for certain source files, possbily preventing striff-lib from generating 
complete and accurate diagrams.

## Status: **Accepted**

## Approaches

### 1. Propagate compile failure info to users.
In this solution, striff-lib ignores any compile failures reported by clarpse during the diagram 
generation process. However, these failures are forwarded alongside any generated diagrams to 
users to give them the appropriate context in case they wonder why certain components are 
missing from generated diagrams. An advantage of this solution is that consumer applications of
striff-lib can decide on how they want to proceed based on the reported list of failures, rather 
than striff-lib make an opinionated decision (e.g. to not generate the diagram). Another aspect of 
this approach that is both an advantage and a disadvantage is that diagrams will get generated 
everytime, but they may not be very accurate at all. 

### 2. Don't generate diagrams for any compile failures.
In this solution, striff-lib throws an error and does not generate any diagrams if any compile 
failures exist in the source code. The benefits of this approach is that we are confident in 
generating accurate striffs. The disadvantage of this approach is that errors in clarpse may 
potentially prevent many users from generating any striffs at all, and it is taking
an opinionated decision that may negatively impact users of striff-lib.

## Decision
**Approach #1**

**Rationale:** Striff-lib is meant to be used an API, and is not a user facing library on its 
own. Therefore, providing consumers with data to take their own decision rather than making 
opinionated decisions seems like the best approach for striff-lib. While generated diagrams have 
the potential to be inaccurate, we are confident that the parser striff-lib uses to parse 
code (currently Clarpse) is well tested and good enough to make this a very rare occurrence. 
Additionally, in most cases, compile errors are small enough to not have any impact on generated 
striffs, making approach #2 an overkill.



