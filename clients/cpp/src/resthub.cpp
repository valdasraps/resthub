
#include "resthub.h"

#include <curl/curl.h>

#include <cassert>

#include "json.hpp"

using json = nlohmann::json;

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

  return req;
}

Response Resthub::folders()
{
  Request* req = get("tables");

  return req;
}

Response Resthub::tables(string folder)
{
  Request* req = get("tables");

  Response r(req);

  json resp = json::parse(r.str());

  json contents = resp[folder];

  /// FIXME: evil request mangling
  req->m_data_in.str(contents.dump());
  return r;
}

Response Resthub::table(string folder, string table)
{
  Request* req = get("table/"+folder+"/"+table);

  return req;
}

Response Resthub::table_cache(string folder, string table)
{
  Request* req = get("table/"+folder+"/"+table+"/cache");

  return req;
}

Response Resthub::blacklist()
{
  return get("blacklist");
}

Response Resthub::blacklist(string folder)
{
  return get("blacklist/"+folder);
}

Response Resthub::blacklist(string folder, string table)
{
  return get("blacklist/"+folder+"/"+table);
}

Response Resthub::blacklist_delete()
{
  return delete_("blacklist");
}

Response Resthub::blacklist_delete(string folder)
{
  return delete_("blacklist/"+folder);
}

Response Resthub::blacklist_delete(string folder, string table)
{
  return delete_("blacklist/"+folder+"/"+table);
}

Query Resthub::query(string sql)
{
  Request* req = post("query", sql);
  req->header("Content-Type", "text");
  Response r(req);

  return Query(this, r.str());
}

vector<Query> Resthub::queries()
{
  Request* req = get("queries");
  Response r(req);

  json a = json::parse(r.str());

  vector<Query> queries;

  for (json::iterator it = a.begin(); it != a.end(); ++it) {
    queries.push_back(Query(this, it.key(), false));
  }
  return queries;
}

Request* Resthub::get(string path, map<string, string> params)
{
  Request * req = new GetRequest(m_server_url + path);

  requests.insert(req);

  return req;
}

Request* Resthub::post(string path, string data)
{
  Request * req = new PostRequest(m_server_url + path, data);

  requests.insert(req);

  return req;
}

Request* Resthub::delete_(string path)
{
  Request * req = new DeleteRequest(m_server_url + path);

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
