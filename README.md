# striff
 
## Introduction

The purpose of this document is to document the requirements for a structural-diff tool called striff. Like a regular 
diff, striff generates striff diagrams which illustrate what is being introduced and discarded from a system as a result
of software patch. However, unlike a regular diff, it does this at a higher, system design level and not at the level
 of source code.

## Striff Diagrams

Given a primary set of source files  P  written in language  L and a secondary set of source files S representing another 
version of P, striff generates a diagram that illustrates the "structural" modifications required to transform P into S.
We'll explain what we mean by "structural" modifications using a sample striff diagram below:




As you may have already noticed, Striff diagrams look a lot like tradional UML class diagrams, the specifications of which
 you can review here. The most important point however, is that components are colored to indicate some noteworthy aspect
  about the transformation from P to S using the follow color scheme:
- Green: Component introduced into the system
- Red: Component removed from the system
- Purple:  Component modified in the system
- White: Component has not changed changed as a result of transforming P into S. As is the case in regular diffs, these components serve an important function of providing context to the diagram. 

## Readability

Readability is a core concern in striff diagrams, and a great deal of design decisions have been made around it. The most important ones are presented below:
- Limiting diagram size: Striff diagrams may not display all the structural modifications that exist between two sets of source files. This is because striff diagrams are designed to provide a high level overview, and can be configured with a cap on the number of components displayed in a diagram. It is important to note however, that striff will always display components related to the transformation in order of importance. See the section below on ranking component importance for more information:
-  Omitting component details: If a component has a large number of attributes, only modified attributes will be displayed in the striff diagram. Additionally, the overall component is marked with a foo symbol to indicate that all attributes are not displayed.
- Including component Source Code comments:  This is one of the major areas striff diagrams differ from traditional UML class diagram - striff diagrams display component comments from source code to better communicate system architecture.

## Ranking Component Importance

As mentioned previously, not all components involved in transforming the original set of source files into the final version may be displayed in a striff diagram due to size constraints. Therefore, striff must find a method for prioritizing the display of the most "important" components, which is pretty difficult to do well. We define the most important diagrams as those components which communicate the structural changes being made to the system to greatest degree. With that said, Striff implements a simple ranking system to determine the importance of components based on the following criteria:
1. The type of class relationships  the component is involved in (realization, inheritance, etc...)
2. Number of class relationships the component is involved in
3. The type of component (class, interface, etc..) the given component is