
#include "Response.h"

#include <cassert>

using namespace std;

Response::Response(Request* req)
{
  m_req = req;
}

bool Response::ok()
{
  return m_req->ok();
}

string Response::str()
{
  assert(ok());
  return m_req->m_data_in.str();
}
