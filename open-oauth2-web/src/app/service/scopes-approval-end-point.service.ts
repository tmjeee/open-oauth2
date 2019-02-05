import {Injectable} from "@angular/core";
import {Observable} from "rxjs";
import {HttpClient} from "@angular/common/http";
import {Config, ConfigLoaderService} from "./config-loader.service";

export interface Res {
  ok:boolean;
  messages:string[],
  redirect_uri:string;
}

@Injectable()
export class ScopesApprovalEndPointService {

  constructor(private httpClient:HttpClient, private configLoaderService:ConfigLoaderService) {}

  approve(selected_scopes:string[], transactionId:string):Observable<Res> {
    let config:Config = this.configLoaderService.config;
    let uri:string = config.oauth2Server+config.resourceOwnerScopesApprovalUrl;
    let formData:FormData = new FormData();
    formData.set("transaction_id", transactionId);
    selected_scopes.forEach((scope:string)=>formData.append("selected_scope", scope));
    return this.httpClient.post<Res>(uri, formData)
  }
}
