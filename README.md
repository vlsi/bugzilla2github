About
=====

This project performs BugZilla to GitHub migration.

See also
--------

https://github.com/Quuxplusone/BugzillaToGithub


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
