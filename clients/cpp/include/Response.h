
#ifndef RESPONSE_H
#define RESPONSE_H

#include "Request.h"

#include "json.hpp"
using json = nlohmann::json;

namespace resthub {

/*!
 * \brief Accesses a rest response
 */
class Response {
  Request* m_req;
public:
  Response() : m_req(0) {}
  Response(Request* req);
  Response(const Response&) = delete;
  Response(Response&& rhs);
  ~Response();

  Response& operator=(Response&& rhs);

  /*!
   * \brief True if request succeeded and 2XX http code.
   * \return
   */
  bool ok()
  {
    return m_req->ok();
  }

  /*!
   * \brief Request http code
   * \return
   */
  int http_code()
  {
    return m_req->http_code();
  }

  /*!
   * \brief Response in string, only exists if ok()
   * \return
   */
  string str();

  /*!
   * \brief Error in string, only exists if !ok()
   * \return
   */
  string error_str();

  /*!
   * \brief Parse response to int
   * \return
   */
  long to_int() {
    return std::stol(str());
  }

  /*!
   * \brief Parse response to float
   * \return
   */
  double to_float() {
    return std::stof(str());
  }

  /*!
   * \brief Parse response to json
   * \return
   */
  json to_json() {
    return json::parse(str());
  }
};

}

#endif // RESPONSE_H
