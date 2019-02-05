import {Injectable} from "@angular/core";
import {Observable} from "rxjs";
import {HttpClient} from "@angular/common/http";
import {Config, ConfigLoaderService} from "./config-loader.service";

export interface Res {
  ok:boolean,
  messages:string[]
}

export type Entry = {
  client_id:string,
  client_secret:string,
  email:string,
  description:string,
  expiration:number,
  confidential:boolean,
  grant_type:string[],
  scope:string[],
  redirect_uri:string[]
}

const URI_REGISTER_CLIENT = "/oauth2/register-client";

@Injectable()
export class RegisterClientEndPointService {

  constructor(private httpClient:HttpClient, private configLoaderService:ConfigLoaderService){}


  registerClient(client:Entry):Observable<Res> {
    let config:Config = this.configLoaderService.config;
    let uri:string = config.oauth2Server+URI_REGISTER_CLIENT;
    let body:FormData = new FormData();
    body.set("client_id", client.client_id);
    body.set("client_secret", client.client_secret);
    body.set("email", client.email);
    body.set("description", client.description);
    body.set("expiration", ''+client.expiration);
    body.set("confidential", ''+client.confidential);
    client.grant_type.forEach((g:string)=>body.append("grant_type", g));
    client.scope.forEach((s:string)=>body.append("scope", s));
    client.redirect_uri.forEach((r:string)=>body.append("redirect_uri",r));
    return this.httpClient.post<Res>(uri, body);
  }

}
