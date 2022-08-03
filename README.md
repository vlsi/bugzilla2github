[![GitHub CI](https://github.com/vlsi/bugzilla2github/actions/workflows/test.yml/badge.svg?branch=main)](https://github.com/vlsi/bugzilla2github/actions/workflows/test.yml)

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
* Duplicates, duplicated by, depends on, and blocks links are converter do GitHub issue links

Known limitations
-----------------

* All issues and comments are imported under the same account, so real names have to be put in the comment text.
  An alternative import approach is to import data to [Gitlab](https://about.gitlab.com/) instance first, and then ask GitHub support to migrate the data.
  However, that requires GitHub support which is not always available.
* The users in GitHub do not automatically subscribe to the issues they were subscribed in Bugzilla
* The votes in Bugzilla are not imported (they are listed in the issue description)

Usage
-----

### 1. Configure URLs

Adjust `bugzilla-backend/src/main/resources/application.conf` as needed.

Here's a sample configuration:

```hocon
ui {
  port = 8081
  host = "0.0.0.0"
}
github {
  # Prefer GITHUB_TOKEN environment variable instead
  # token = "..."
  organization = "vlsi"
  attachments-repository = "tmp-jmeter-attachments"
  issues-repository = "tmp-jmeter-issues"
}
bugzilla {
  url = "https://bz.apache.org/bugzilla/"
  product = "JMeter"
  database {
    name = "bugzilla"
    username = "root"
    # Prefer BUGZILLA_DB_PASSWORD environment variable instead
    # password = "root"
    host = "localhost"
    # type = "mysql"
    # port = 3306
    # driver = "com.mysql.cj.jdbc.Driver"
    # url = "jdbc:mysql://localhost:3306/bugzilla"
  }
}
```

### 2. Export attachments from Bugzilla

```sh
./gradlew :bugzilla-backend:run --args="export-attachments --data-folder=build/attachments"
cd build/attachments
git init
git commit --allow-empty -m "Initial commit"
git checkout -b gh-pages
git remote add origin https://github.com/$organization/$attachmentRepository.git
git push origin gh-pages
```

### 2. Create bug_id <-> issue_number mapping

Your GitHub repository might already have some issues (or pull requests),
and the next will use sequential indices. So you need to figure you the latest `issue_number` and create the mapping.

You can fetch the latest issue number via the following API:

    https://api.github.com/repos/$organization/$repository/issues?per_page=1

Then you need to create the mapping table:

```sh
./gradlew :bugzilla-backend:run --args="map-bugs-to-issues --latest-issue-number=0"
```

The created `conv_bug_issues` table might be helpful to post-process existing bugs.
For instance, the following query would add comment that says the bug has been migrated

```sql
insert into long_descs (bug_id, who, bug_when, thetext)
  select c.bug_id, p.userid, now(), 'This bug has been migrated to GitHub: https://github.com/organization/repository/issues/'||c.issue_number
    from conf_bug_issues c, profiles p
   where p.login_name = '...'
```

### 3. Preview the conversion results

This is optional, however, it might be convenient to preview the results before you upload issues to GitHub.

The following command would spawn a web server that could serve the bug description.

```sh
./gradlew :bugzilla-backend:run -PwithFrontend=true --args="backend"
```

### 4. Import bugs to GitHub

> **Warning**
> This is NOT reversible. You can't easily delete issues from repository, so please test
> the procedure with a temporary repository first.

> **Note**
> Each issue import takes 1-2 seconds

> **Note**
> You can add `--dry-run` to see what would be imported without calling GitHub import APIs

```sh
./gradlew :bugzilla-backend:run --args="import-to-github"
```

If the process fails, you could resume the import as follows (assume bug 50032 was imported successfully):

```sh
./gradlew :bugzilla-backend:run --args="import-to-github --first-bug-id=50032"
```

Sample results
--------------

* https://github.com/vlsi/tmp-jmeter-issues/issues

License
-------
Apache License 2.0

Change log
----------

Author
------
Vladimir Sitnikov <sitnikov.vladimir@gmail.com>
