[![GitHub CI](https://github.com/pgjdbc/pgjdbc/actions/workflows/main.yml/badge.svg?branch=master)](https://github.com/pgjdbc/pgjdbc/actions/workflows/main.yml)

About
=====

This project performs BugZilla to GitHub migration.

See also
--------

* https://github.com/Quuxplusone/BugzillaToGithub
* https://github.com/llvm/bugzilla2gitlab/tree/llvm/bugzilla2gitlab

Features
--------

* UI for preview of the conversion without exporting data to GitHub
* Export attachments from Bugzilla database to a folder, so the attachments could be served from [GitHub Pages](https://pages.github.com/).
* Uses [GitHub bulk import issue API](https://gist.github.com/jonmagic/5282384165e0f86ef105), so issues and comments are imported with proper timestamps
* Images and small code snippets are displayed inline in the GitHub comments.

Known limitations
-----------------

* All issues and comments are imported under the same account, so real names have to be put in the comment text.
  An alternative import approach is to import data to [Gitlab](https://about.gitlab.com/) instance first, and then ask GitHub support to migrate the data.
  However, that requires GitHub support which is not always available.
* The users in GitHub do not automatically subscribe to the issues they were subscribed in Bugzilla
* The votes in Bugzilla are not imported (they are listed in the issue description)

Sample usage
------------

The following command would fetch ten bugs from Bugzilla and store them into `build/data` folder.

    $ ./gradlew :bugzilla-export:run --args="fetch-bugs --limit=10 --login=test --host=https://bz.apache.org/bugzilla --product=JMeter --data-folder=build/data"

License
-------
Apache License 2.0

Change log
----------

Author
------
Vladimir Sitnikov <sitnikov.vladimir@gmail.com>
