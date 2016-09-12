
#include "Request.h"

class Response {
  Request* m_req;
public:
  Response() : m_req(0) {}
  Response(Request* req);

  bool ok();


  string str();

};
