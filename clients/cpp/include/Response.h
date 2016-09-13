
#ifndef RESPONSE_H
#define RESPONSE_H

#include "Request.h"

namespace resthub {

class Response {
  Request* m_req;
public:
  Response() : m_req(0) {}
  Response(Request* req);
  Response(const Response&) = delete;
  Response(Response&& rhs);
  ~Response();

  Response& operator=(Response&& rhs);

  bool ok();

  string str();

  long to_int() {
    return std::stol(str());
  }

  double to_float() {
    return std::stof(str());
  }
};

}

#endif // RESPONSE_H
