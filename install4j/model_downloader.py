#! /usr/bin/env python3
# Author: Arne Köhn <arne@chark.eu>
# License: Apache 2

# downloads the pocketsphinx models and extracts them for future use by install4j.
from io import BytesIO
from zipfile import ZipFile
import os
import requests
import simplejson
import sys

def eprint(*args, **kwargs):
    print(*args, file=sys.stderr, **kwargs)


currdir = os.path.abspath(os.path.dirname(sys.argv[0]))
modeldict = {}
# workaround until the json file is valid.  Download by hand, remove the newline in the string in line 5, save as
# pocketsphinx.json
if False:
    modeldict = requests.get("http://www.coli.uni-saarland.de/~koller/dialogos/models/pocketsphinx.json").json()
else:
    modeldict = simplejson.load(open("pocketsphinx.json"))

for name, model in modeldict['dialogos.plugin.pocketsphinx'].items():
    eprint("downloading " + model['name'])
    data = requests.get(model["url"]).content
    outdir = currdir + "/models/" + name
    os.mkdir(outdir)
    ZipFile(BytesIO(data)).extractall(outdir)
