
#include <cassert>

using namespace std;

#include <curl/curl.h>

#include "Request.h"

#include "resthub.h"

#include <regex>
#include <iostream>

Request::Request(string url, map<string, string> params = {})
  :m_state(INIT)
{
  m_curl = curl_easy_init();

  if(params.size()) {
    url += "?";
    bool not_first = false;
    for(pair<string,string> p : params) {
      if(not_first) {
        url += "&";
      }

      url += p.first + "=" + p.second;

      not_first = true;
    }
  }

  m_url = url;

  curl_easy_setopt(m_curl, CURLOPT_URL, m_url.c_str());

  curl_easy_setopt(m_curl, CURLOPT_WRITEFUNCTION, &Request::write_data);
  curl_easy_setopt(m_curl, CURLOPT_WRITEDATA, this);

  //curl_easy_setopt(m_curl, CURLOPT_VERBOSE, true);
}

Request::~Request()
{
  curl_easy_cleanup(m_curl);
}

Request::State Request::state()
{
  return m_state;
}

bool Request::ok()
{
  if(state() != ERROR && state() < RUNNING) {
    perform();
  }

  return state() == DONE;
}

size_t Request::write_data(void* buffer, size_t size, size_t nmemb, Request* req)
{
  cout << "Writing " << size*nmemb << " bytes. " << endl;
  return req->m_data.sputn((const char*)buffer, size * nmemb);
}

void Request::perform()
{
  m_state = RUNNING;
  cout << method_str() <<"ing " << m_url << endl;
  CURLError::check(curl_easy_perform(m_curl));
  m_state = DONE;
}

GetRequest::GetRequest(string url, map<string, string> params)
  :Request(url, params)
{
  perform();
}
