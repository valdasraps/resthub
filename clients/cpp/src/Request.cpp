
#include "resthub.h"
using namespace resthub;

#include <cassert>
#include <regex>
#include <iostream>

using namespace std;

#include <curl/curl.h>

#include "Request.h"


void Request::link_resthub(Resthub* rh) {
  assert(m_resthub == 0);
  m_resthub = rh;
  m_resthub->add_req(this);

  curl_easy_setopt(m_curl, CURLOPT_VERBOSE, m_resthub->verbose());
}

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
}

Request::~Request()
{
  if(m_resthub)
    m_resthub->del_req(this);

  curl_slist_free_all((curl_slist*)m_curl_header_list);
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

  return state() == DONE && (m_http_code / 100) == 2;
}

int Request::http_code()
{
  // Run request, if not run yet.
  ok();

  return m_http_code;
}

void Request::header(string header, string value)
{
  bool add_header_list = m_curl_header_list == 0;
  m_curl_header_list = curl_slist_append((curl_slist*)m_curl_header_list, (header+": "+value).c_str());

  if(add_header_list)
    curl_easy_setopt(m_curl, CURLOPT_HTTPHEADER, (curl_slist*)m_curl_header_list);
}

size_t Request::write_data(void* buffer, size_t size, size_t nmemb, Request* req)
{
  return req->m_data_in.sputn((const char*)buffer, size * nmemb);
}

size_t Request::read_data(char* buffer, size_t size, size_t nitems, Request* req)
{
  return req->m_data_out.sgetn(buffer, size*nitems);
}

void Request::enable_output()
{
  curl_easy_setopt(m_curl, CURLOPT_POST, 1);
  curl_easy_setopt(m_curl, CURLOPT_POSTFIELDS, 0);
  curl_easy_setopt(m_curl, CURLOPT_READFUNCTION, &Request::read_data);
  curl_easy_setopt(m_curl, CURLOPT_READDATA, this);
}

void Request::perform()
{
  m_state = RUNNING;

  if(m_resthub && m_resthub->verbose())
    cerr << method_str() <<"ing " << m_url << endl;

  /// Cannot have chunked streams to resthub.
  curl_easy_setopt(m_curl, CURLOPT_POSTFIELDSIZE, m_data_out.str().size());
  CURLError::check(curl_easy_perform(m_curl));
  curl_easy_getinfo(m_curl, CURLINFO_RESPONSE_CODE, &m_http_code);
  m_state = DONE;
}

GetRequest::GetRequest(string url, map<string, string> params)
  :Request(url, params)
{
}

PostRequest::PostRequest(string url, string data)
  : Request(url, {})
{
  enable_output();
  m_data_out.sputn(data.c_str(), data.size());
}

DeleteRequest::DeleteRequest(string url)
  : Request(url, {})
{
  curl_easy_setopt(m_curl, CURLOPT_CUSTOMREQUEST, "DELETE");
}
