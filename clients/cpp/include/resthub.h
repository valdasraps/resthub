
#ifndef RESTHUB_H
#define RESTHUB_H

#include <string>
#include <map>
#include <set>
#include <vector>

using std::string;
using std::vector;
using std::map;

#include "Request.h"
#include "Response.h"
#include "Query.h"

class CURLError : public std::runtime_error{
public:
  CURLError(int curl_code);

  static void check(int curl_code);
};

class Resthub {

public:
  Resthub(std::string url);
  ~Resthub();

 Response info();
 Response folders();
 Response tables(string folder);
 Response table(string folder, string table);
 Response table_cache(string folder, string table);

 Response blacklist();
 Response blacklist(string folder);
 Response blacklist(string folder, string table);

 Response blacklist_delete();
 Response blacklist_delete(string folder);
 Response blacklist_delete(string folder, string table);

 Query query(string sql);

 vector<Query> queries();

private:
 // Data members
 void* m_curl_h;
 std::string m_server_url;

 // Classes

 std::set<Request*> requests;

 // Methods
 Request* get(string path, map<string, string> params = {});
 Request* post(string path, string data);
 Request* delete_(string path);

 friend class Query;
};

#endif
