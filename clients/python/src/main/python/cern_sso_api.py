import sys
import json
from getpass import getpass
from os import remove, path
import requests
import warnings
import logging
from ilock import ILock
from base64 import b64encode, b64decode

from selenium import webdriver
from selenium.webdriver.firefox.options import Options
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC

def file_mtime(file_path):
    try:
        return path.getmtime(file_path)
    except OSError:
        return -1

def cern_sso_cookies(url, cache_file = ".session.cache", force_level = 0):

    cache = None
    cache_file = path.abspath(cache_file)
    cache_lock_id = b64encode(cache_file.encode('utf-8')).decode()
    cache_time = file_mtime(cache_file)

    with ILock(cache_lock_id):

        if force_level == 1 and cache_time != file_mtime(cache_file):
            force_level = 0

        if force_level == 2:
            remove(cache_file)

        if path.isfile(cache_file):

            logging.debug('%s found', cache_file)
            with open(cache_file, 'r') as f:
                cache = json.loads(f.read())

        else:

            logging.debug('%s not found', cache_file)

        if force_level > 0 or cache is None or 'cookies' not in cache:

            if cache is not None and 'secret' in cache:
                secret = b64decode(cache['secret'].encode()).decode()
                username, password = secret.split('/')
                password = b64decode(password.encode()).decode()
            else:
                username = input("Username: ")
                password = getpass("Password: ")
                logging.warning('Credentials will be stored in a NOT secure way!')

            options = Options()
            options.headless = True
            driver = webdriver.Firefox(options = options)
            driver.get(url)

            in_login = driver.find_element_by_xpath("//td[@class='box_login']//input")
            in_login.clear()
            in_login.send_keys(username)
            in_passw = driver.find_element_by_xpath("//td[@class='box_password']//input")
            in_passw.clear()
            in_passw.send_keys(password)
            in_submit = driver.find_element_by_xpath("//td[@class='box_signinbutton']//input")
            in_submit.click()

            WebDriverWait(driver, 10).until(EC.url_to_be(url))

            cache = {
                'secret': b64encode((username + '/' + b64encode(password.encode()).decode()).encode()).decode(),
                'location': driver.current_url,
                'cookies': { i['name']: i['value'] for i in driver.get_cookies() }
            }

            driver.close()

            with open(cache_file, 'w') as f:
                f.write(json.dumps(cache))

    return cache['cookies']

def cern_api(url):

    force_level = 0
    while True:

        cookies = cern_sso_cookies(url, force_level = force_level)

        warnings.simplefilter("ignore")
        r = requests.get(url, cookies = cookies, verify = False)

        if r.status_code == 200:
            if r.url.startswith('https://login.cern.ch/') and force_level < 2:
                force_level = force_level + 1
                continue
            else:   
                return r.content
        
        raise Exception(r)

if __name__ == "__main__":

    logging.basicConfig(level = logging.INFO)

    if len(sys.argv) == 1:
        print("Usage:", sys.argv[0], "api-url")
    else:
        print(cern_api(sys.argv[1]))

