= Built on CentOS Linux release 7.9.2009 (Core) x86_64

tar xfz venv.tar.gz 
source venv/bin/activate
python3 rhapi.py --url=https://cmsdca.cern.ch/trk_rhapi --login
deactivate

