# Google Java Style #

The convention is directly copied from [Google Java Style Guides](https://github.com/google/styleguide/blob/gh-pages/intellij-java-google-style.xml). Any
submission to the project should have the code formatted following this
convention.

Configuration of formatting tools for IntelliJ IDEA is included along with the
project as can be setup as follow.

## Code Style ##

This is a built-in configuration for Intellij the configuration is in the `documents/codestyles/intellij-google-style.xml`

1. Press `Ctr-Alt-S` to open settings select `Editor` > `Code Style` > `Java`.
2. Select `Project` level scheme and press the gear icon to import the configuration file mentioned above

[Official Documentation](https://www.jetbrains.com/help/idea/configuring-code-style.html#create-copy)

## CheckStyle-IDEA ##

This is a plugin. After install, should config the style to use the project
`checkstyle.xml` file. Adding the included config can be done as follow

1. Press `Ctr-Alt-S` to open settings select `Tool` > `Checkstyle`.
2. Select `+` at the `Configuration File` panel
3. Browse the `checkstyle.xml` file

[Official Documentation](https://github.com/jshiell/checkstyle-idea/blob/main/README.md)
