
#ifndef RESTHUB_H
#define RESTHUB_H

#include <string>
#include <map>
#include <set>

using std::string;
using std::map;

#include "Request.h"
#include "Response.h"

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
 Response tables(std::__cxx11::string folder);
 Response table();
 Response qid();
 Response query();
 Response count();
 Response data();

 Response csv();
 Response xml();
 Response json();
 Response json2();

private:
 // Data members
 void* m_curl_h;
 std::string m_server_url;

 // Classes

 std::set<Request*> requests;

 // Methods
 Request* get(string path, map<string, string> params = {});
 Request* post(string url);
};

#endif
