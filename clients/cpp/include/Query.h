
#ifndef QUERY_H
#define QUERY_H

#include <string>
#include <map>

#include "Response.h"

using std::string;

class Resthub;

class Query {

  friend class Resthub;

  Query(Resthub* parent, string id, bool owner = true);
public:
  Query(const Query&) = delete;
  Query(Query&& rhs);
  ~Query();

  string id() {
    return m_id;
  }

  string str() {
    return  "Query( id="+m_id+" )";
  }

  Response csv(map<string, string> params = {}, int page = -1, int rows_per_page = -1)
  {
    return data_req("text/csv", params, page, rows_per_page);
  }

  Response xml(map<string, string> params = {}, int page = -1, int rows_per_page = -1)
  {
    return data_req("text/xml", params, page, rows_per_page);
  }

  Response json(map<string, string> params = {}, int page = -1, int rows_per_page = -1)
  {
    return data_req("application/json", params, page, rows_per_page);
  }

  Response json2(map<string, string> params = {}, int page = -1, int rows_per_page = -1)
  {
    return data_req("application/json2", params, page, rows_per_page);
  }

  Response cache();
  Response cache_delete();

  Response function(string func);
private:
  Resthub* m_resthub;
  string   m_id;
  bool     m_owner; // Does this object own the query on the server.

  Request* data_req(string data_type, map<string, string> params = {}, int page = -1, int rows_per_page = -1);
};

#endif // QUERY_H
