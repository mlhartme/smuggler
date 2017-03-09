# Smuggler

is a command line tool to upload a folder to your Smugmug account and keep it in sync.
And a Smugmug Api, using version 2.0.

Commands:

`smuggler sync` synchronizes the local folder with your account: local-only files are uploaded,
  remote-only files are removed locally. This command uses a local index file to keep track of all remote
  files.

`smuggler index` re-builds the local index file by loading a full file list from remote

