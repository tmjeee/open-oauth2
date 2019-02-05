import {Injectable} from "@angular/core";
import {Observable} from "rxjs";
import {HttpClient} from "@angular/common/http";
import {Config, ConfigLoaderService} from "./config-loader.service";

export interface Res {
  ok:boolean;
  messages:string[];
  redirect_uri:string;
};

@Injectable()
export class AuthenticationEndPointService {

  constructor(private httpClient:HttpClient,
              private configLoaderService:ConfigLoaderService) {}


  authenticate(input:{transactionId:string, username:string, password:string}):Observable<Res>  {
    let config:Config = this.configLoaderService.config;
    let post_url:string = config.oauth2Server+config.resourceOwnerAuthenticationUrl;
    let formData:FormData = new FormData();
    formData.set("transaction_id", input.transactionId);
    formData.set("username", input.username);
    formData.set("password", input.password);

    return this.httpClient
      .post<Res>(post_url, formData);
  }




}
