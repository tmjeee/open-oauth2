import {Injectable} from "@angular/core";
import {Config, ConfigLoaderService} from "./config-loader.service";
import {Observable} from "rxjs";
import {HttpClient} from "@angular/common/http";

export interface Res {
  ok:boolean;
  messages:string[],
  redirect_uri:string;
}

@Injectable()
export class ScopesRejectionEndPointService {

  constructor(private httpClient:HttpClient, private configLoaderService:ConfigLoaderService) {}

  reject(transactionId:string):Observable<Res> {
    let config:Config = this.configLoaderService.config;
    let uri:string = config.oauth2Server+config.resourceOwnerScopesRejectionUrl;
    let formData:FormData = new FormData();
    formData.set("transaction_id", transactionId);
    return this.httpClient.post<Res>(uri, formData)
  }

}
