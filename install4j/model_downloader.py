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
if False:
    modeldict = requests.get("http://www.coli.uni-saarland.de/~koller/dialogos/models/pocketsphinx.json").json()
else:
    modeldict = simplejson.load(open("pocketsphinx.json"))

for model in modeldict['dialogos.plugin.pocketsphinx'].values():
    eprint("downloading " + model['name'])
    data = requests.get(model["url"]).content
    ZipFile(BytesIO(data)).extractall(currdir+"/models")
