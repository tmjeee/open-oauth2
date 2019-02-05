import {Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {Observable, of} from "rxjs";

export interface Scopes {
  scopes:string[];
}

@Injectable()
export class ScopesEndPointService {

  constructor(private httpClient:HttpClient) {
  }

  getScopes(transactionId:string):Observable<Scopes> {
    return of(<Scopes>{scopes:["scope1", "scope2", "scope3"]});
  }

}
