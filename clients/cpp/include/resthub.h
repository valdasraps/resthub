
#ifndef RESTHUB_H
#define RESTHUB_H

#include <string>
#include <map>
#include <set>
#include <vector>

#include "Request.h"
#include "Response.h"
#include "Query.h"

namespace resthub {

using std::string;
using std::vector;
using std::map;

class CURLError : public std::runtime_error{
public:
  CURLError(int curl_code);

  static void check(int curl_code);
};

/*!
 * \brief Main Resthub API class
 */
class Resthub {

public:
  Resthub(std::string url);
  ~Resthub();

  /*!
  * \brief Server info
  * \return
  */
 Response info();
 Response folders();
 Response tables(string folder);
 Response table(string folder, string table);
 Response table_cache(string folder, string table);

 /*!
  * \brief Access blacklist
  * \return
  */
 Response blacklist();
 Response blacklist(string folder);
 Response blacklist(string folder, string table);

 /*!
  * \brief Remove blacklist items
  * \return
  */
 Response blacklist_delete();
 Response blacklist_delete(string folder);
 Response blacklist_delete(string folder, string table);

 /*!
  * \brief Create query
  * \param sql SQL statement
  * \return Query wrapper
  */
 Query query(string sql);

 /*!
  * \brief List of all queries on server.
  * \return
  */
 vector<Query> queries();

 bool verbose() {
   return m_verbose;
 }

 void verbose(bool verbose) {
   m_verbose = verbose;
 }

private:
 // Data members
 void* m_curl_h;
 std::string m_server_url;

 bool m_verbose = false;

 // Request tracking
 std::set<Request*> m_requests;
 void add_req(Request* req);
 void del_req(Request* req);
 friend class Request;

 // Methods
 Request* get(string path, map<string, string> params = {});
 Request* post(string path, string data);
 Request* delete_(string path);

 friend class Query;
};

}
#endif
