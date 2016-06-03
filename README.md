# scalatype
## Makes inferred types explicit

#### Problem:

In a code base, often times bug fixing is assigned to new developers. These developers appreciate having explicit types because it makes the code easier to understand. This command line utility inserts the types explicitly into your SBT project so that you [maintainer] don't have to.

#### Intended usage:

Open up a terminal in the root directory of your SBT project and run it. scalatype takes care of the rest.

#### Features:

- Leave out type whose name is already given on the right hand side. val cat = new Cat() // no explicit type necessary.
- Leave out declaration of primatives. val str = "string" // no explicit type necessary.
- Undo last run.

#### Command line options:

- noRepeats // Do not repeat a type on the right hand side
- noPrimatives // Do not make explicit types for declaration of primatives
- undo // Undoes the last operation
