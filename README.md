# Goal
Hermes is an extendable all purpose file and message transport library for Java.
The intent is to abstract different file and message transport layers to a simpler higher level API
as well as provide more sophisticated use cases like:
* recursive up and download,
* local and remote pre and post processing
* ...

### Quickstart
just clone the git repo to your local machine and import each module as a maven project into your IDE.

# Hermes Core
Hermes core provides the foundation for the other Hermes modules. It already includes a local file system connection.

# Hermes Ftp
Utilising the apache commons-net FTPClient  hermes-ftp provides connections to FTP servers.

# Todos
* remote file systems
** provide FTPS support
** provide SFPT support
** provide WEB-Dav support
