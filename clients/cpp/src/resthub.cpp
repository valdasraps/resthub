
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
  if(m_requests.size() > 0) {
    cerr << "WARNING: Resthub requests unbalanced at end of Resthub lifetime." << endl;
    for(Request* req : m_requests) {
      delete req;
    }
  }

  assert(CURLM_OK == curl_multi_cleanup(m_curl_h));

  curl_global_cleanup();
}

Response Resthub::info()
{
  return get("info");
}

Response Resthub::folders()
{
  return get("tables");
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
  return get("table/"+folder+"/"+table);
}

Response Resthub::table_cache(string folder, string table)
{
  return get("table/"+folder+"/"+table+"/cache");
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

void Resthub::add_req(Request* req)
{
  m_requests.insert(req);
}

void Resthub::del_req(Request* req)
{
  m_requests.erase(req);
}

Request* Resthub::get(string path, map<string, string> params)
{
  auto* req = new GetRequest(m_server_url + path);
  req->link_resthub(this);
  return req;
}

Request* Resthub::post(string path, string data)
{
  auto* req = new PostRequest(m_server_url + path, data);
  req->link_resthub(this);
  return req;
}

Request* Resthub::delete_(string path)
{
  auto* req = new DeleteRequest(m_server_url + path);
  req->link_resthub(this);
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
