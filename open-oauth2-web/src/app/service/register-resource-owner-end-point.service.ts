import {Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {Config, ConfigLoaderService} from "./config-loader.service";

export interface Res {
  ok:boolean,
  messages:string[];
}

const URI_REGISTER_RESOURCE_OWNER:string = "/oauth2/register-resource-owner";

@Injectable()
export class RegisterResourceOwnerEndPointService {


  constructor(private httpClient:HttpClient, private configLoaderService:ConfigLoaderService) {
  }


  registerResourceOwner(resourceOwner:{username:string, password:string, email:string, confirmPassword:string, autoApprove:boolean}):Observable<Res> {
    let config:Config = this.configLoaderService.config;
    let url:string = config.oauth2Server+URI_REGISTER_RESOURCE_OWNER;
    let body:FormData = new FormData();
    body.set("username", resourceOwner.username);
    body.set("password", resourceOwner.password);
    body.set("email", resourceOwner.email);
    body.set("confirmPassword", resourceOwner.confirmPassword);
    body.set("autoApprove", ''+resourceOwner.autoApprove);
    return this.httpClient
      .post<Res>(url,body);
  }

}
