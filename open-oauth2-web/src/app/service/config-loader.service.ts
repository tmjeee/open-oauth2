import {Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {map} from "rxjs/operators";

export interface Config {
  oauth2Server:string,
  resourceOwnerAuthenticationUrl:string,
  resourceOwnerScopesApprovalUrl:string,
  resourceOwnerScopesRejectionUrl:string
};

@Injectable()
export class ConfigLoaderService {

  config:Config;

  init(httpClient:HttpClient) {
    httpClient
      .get("/assets/config.json")
      .pipe(
        map((config:Config)=>{
          this.config = config;
        })
      )
      .subscribe();
  }
}
