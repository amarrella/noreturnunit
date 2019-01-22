# Scalafix example
This is just and example, please don't use it as-is.

We want to make sure user doesn't write methods that return `Unit`.
This repo explores two examples of rules to avoid this to happen.

## Example: linting
In the branch `linter` you will find a simple linting rule that will raise an error if there is any method returning unit in the code.

To run this rule you need to [add scalafix as dependency to your project](https://scalacenter.github.io/scalafix/docs/users/installation.html#sbt) and run:
`sbt ";scalafixEnable; scalafix github:amarrella/noreturnunit/Noreturnunit?sha=linter"`

## Example: replacing with io
In the branch `io` you will find a slightly more complex rule that will replace functions that return `Unit` with functions that return `IO[Unit]`.

To run this rule you need to [add scalafix as dependency to your project](https://scalacenter.github.io/scalafix/docs/users/installation.html#sbt) and run:
`sbt ";scalafixEnable; scalafix github:amarrella/noreturnunit/Noreturnunit?sha=io"`

This is just an small example of what's achievable with Scalafix. For more information check out 