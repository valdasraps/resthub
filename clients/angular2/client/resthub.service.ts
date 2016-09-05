import { Injectable } from '@angular/core';
import { Http, Response, Headers, RequestOptions, URLSearchParams } from '@angular/http';

import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';
import { Observable } from 'rxjs/Rx';

@Injectable()
export class ResthubService {

    constructor(private http: Http) { }

    private resthubUrl = 'http://localhost:2113';


    /*

    Returns Query_ID (string) from ResthubService

    options {
		sql   : "SELECT t.column FROM namespace.table t"
    }

    */

    _query(options) {

        let headers = new Headers({ 'Content-Type': 'text/plain' });

        let sql = "";

        if ('sql' in options) {
            sql = "SELECT * FROM ( " + options.sql + " ) a;";
        }
        else {
            return this.handleError("SQL is not defined");
        }

        return this.http.post(this.resthubUrl + '/query', sql, headers)
            .map((resp: Response) => { 
                let ret = {};
                ret['qid'] = resp.text();
                return ret;
            })
            .catch(this.handleError);
    }

    /*

    Returns Data (json) from ResthubService

    options {
		sql   : "SELECT t.column FROM namespace.table t"
        params: query parameters (if any)     
		type  : "application/json"
		ppage : rows per page 
		page  : page number 

        TODO:  qid   : query ID
	}

    Both 'page' and 'ppage' must be set if used.

    TODO: One of ('sql' or 'qid') is required. Other parameters are optional (not implemented)
     
    */

    data(options) {

        return this._query(options)
            .flatMap((queryResp) => {

                let url = this.resthubUrl + '/query/' + queryResp.qid;                
                
                if (('ppage' in options) && ('page' in options)) {
                    url += '/page/' + options.ppage + '/' + options.page;
                }

                url += "/data";

                // Accept type. Sets default if not provided
                if (!('type' in options)) {
                    options.type = "application/json2"
                }
                
                // URL search parameters
                let params = new URLSearchParams();
                for (var key in options.params){
                    params.set(key, options.params[key]);
                }
                
                // Request options
                let requestOptions = new RequestOptions({
                    headers : new Headers({ 'Accept': options.type }),
                    search  : params 
                })

                return this.http.get(url, requestOptions)
                    .map((resp: Response) => {
                        let ret = resp.json();
                        ret['qid'] = queryResp.qid;
                        return ret;
                    })
                    .catch(this.handleError)
            })

    }

    /*
        Delete query
    */
    delete(qid) {
        let url = this.resthubUrl + '/query/' + qid;
        this.http.delete(url).catch(this.handleError);
    }

    /*
        Delete cache of the query
    */
    clear(qid) {
        let url = this.resthubUrl + '/query/' + qid + '/cache';
        this.http.delete(url).catch(this.handleError);
    }

    handleError(error: any) {
        console.error('Resthub Error: ' + error);
        return Observable.throw(error || 'Server error');
    }

}