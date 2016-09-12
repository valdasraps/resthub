
#ifndef REQUEST_H
#define REQUEST_H

#include <string>
#include <map>
#include <sstream>

using std::string;
using std::map;

class Request {
  void* m_curl;

  string m_url;
  std::stringbuf m_data;

  enum State {
    NONE, INIT, RUNNING, DONE, ERROR
  };

  State m_state;

  friend class Response;
public:
  Request(string url, map<string, string> params);
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
protected:
  static size_t write_data(void* buffer, size_t size, size_t nmemb, Request* req);

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
};

#endif // REQUEST_H
