
#include "resthub.h"

#include <curl/curl.h>

#include <cassert>

using namespace std;

Resthub::Resthub(string server_url)
{
  curl_global_init(CURL_GLOBAL_DEFAULT);

  m_curl_h = curl_multi_init();

  m_server_url = server_url + "/";
}

Resthub::~Resthub()
{

  assert(CURLM_OK == curl_multi_cleanup(m_curl_h));

  curl_global_cleanup();
}

Response Resthub::info()
{
  Request* req = get("info");

  return Response(req);
}

Response Resthub::folders()
{
  Request* req = get("tables");

  return Response(req);
}

Response Resthub::tables(string folder)
{
  Request* req = get("tables");

  /// TODO: filter folder
  return Response(req);
}

Request* Resthub::get(string path, map<string, string> params)
{
  Request * req = new GetRequest(m_server_url + path);

  requests.insert(req);

  return req;
}

CURLError::CURLError(int curl_code)
  :std::runtime_error(curl_easy_strerror((CURLcode)curl_code))
{

}

void CURLError::check(int curl_code)
{
   if(curl_code != CURLE_OK) {
     throw CURLError(curl_code);
   }
}
