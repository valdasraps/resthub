
#ifndef REQUEST_H
#define REQUEST_H

#include <string>
#include <map>
#include <sstream>

namespace resthub {

using std::string;
using std::map;

class Response;
class Resthub;

class Request {

  enum State {
    NONE, INIT, RUNNING, DONE, ERROR
  };

  State m_state;
protected:
  Resthub* m_resthub = 0;
  Response* m_parent = 0;

  void* m_curl;

  void* m_curl_header_list = 0;

  string m_url;
  std::stringbuf m_data_in;
  std::stringbuf m_data_out;

  friend class Response;
  friend class Resthub;

  void link_resthub(Resthub* rh);

  void parent(Response* resp) {
    m_parent = resp;
  }

  Response* parent() {
    return m_parent;
  }

public:
  Request(string url, map<string, string> params);
  Request(const Request&) = delete;
  ~Request();

  enum Method {
    GET, POST, DELETE
  };

  virtual Method method() = 0;

  const char* method_str() {
    switch(method()) {
    case GET:
      return "GET";
    case POST:
      return "POST";
    case DELETE:
      return "DELETE";
    }
  }

  State state();
  bool ok();
  
  void header(string header, string value);

protected:
  static size_t write_data(void* buffer, size_t size, size_t nmemb, Request* req);
  static size_t read_data(char *buffer, size_t size, size_t nitems, Request* req);

  void enable_output();
  void perform();
};

class GetRequest : public Request {
public:
  GetRequest(string url, map<string, string> params = {});

  virtual Method method() {
    return GET;
  }
};

class PostRequest : public Request {
public:
  PostRequest(string url, string data);

  virtual Method method() {
    return GET;
  }
};

class DeleteRequest : public Request {
public:
  DeleteRequest(string url);

  virtual Method method() {
    return DELETE;
  }
};

}
#endif // REQUEST_H
