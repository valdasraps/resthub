
#include "Query.h"

#include "resthub.h"

using namespace std;

Query::Query(Resthub* parent, string id, bool owner)
{
  m_resthub = parent;
  m_id = id;
  m_owner = owner;
}

Query::Query(Query&& rhs)
{
  m_resthub = rhs.m_resthub;
  m_id = rhs.m_id;
  m_owner = rhs.m_owner;
  rhs.m_owner = false;
}

Query::~Query()
{
  if(m_owner) {
    Response r = m_resthub->delete_("query/"+m_id);
  }
}

Response Query::cache()
{
  return m_resthub->get("query/"+m_id+"/cache");
}

Response Query::cache_delete()
{
  return m_resthub->delete_("query/"+m_id+"/cache");
}

Response Query::function(string func)
{
  return m_resthub->get("query/"+m_id+"/"+func);
}

Request*Query::data_req(string data_type, map<string, string> params, int page, int rows_per_page)
{
  Request* req;
  if( page == -1 && rows_per_page == -1)
    req = m_resthub->get("query/"+m_id+"/data", params);
  else
    req = m_resthub->get("query/"+m_id+"/page/"+to_string(rows_per_page)+"/"+to_string(page)+"/data", params);

  req->header("Accept", data_type);

  return req;
}
