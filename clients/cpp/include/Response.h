
#ifndef RESPONSE_H
#define RESPONSE_H


#include "Request.h"

class Response {
  Request* m_req;
public:
  Response() : m_req(0) {}
  Response(Request* req);

  bool ok();


  string str();

  long to_int() {
    return std::stol(str());
  }

  double to_float() {
    return std::stof(str());
  }
};

#endif // RESPONSE_H
