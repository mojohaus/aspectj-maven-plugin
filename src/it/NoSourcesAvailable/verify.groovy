def log = new File(basedir, 'build.log').text
assert log.contains('No sources found skipping aspectJ compile')
