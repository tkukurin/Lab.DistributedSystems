from shutil import copyfile
from glob import glob

import zipfile as zf
import os


subdir_java = glob('./**/*.java', recursive=True)
subdir_mvn = glob('./**/pom.xml', recursive=True)
copy_to = '/home/toni/faks/9/rasus/lab/lab01submit'

for f in subdir_java + subdir_mvn + glob('./**/*.yaml', recursive=True):
  fname = f[f.rindex('/')+1:]
  print('Copying', fname)
  if 'connector' in f or 'service' in f:
    copyfile(f, os.path.join(copy_to, 'server', fname))
  else:
    copyfile(f, os.path.join(copy_to, 'client', fname))

