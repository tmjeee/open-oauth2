import {SelectItem} from "primeng/api";
import {Component} from "@angular/core";
import {OAUTH2_SERVER} from "../../const";

@Component({
  templateUrl:'./test-authorization-end-point.page.html',
  styleUrls:['./test-authorization-end-point.page.scss']
})
export class TestAuthorizationEndPointPage {

  client_id:string;
  response_type:string;
  redirect_uri:string;
  scope:string;
  state:string;
  responseTypes:SelectItem[] = [
    <SelectItem>{label:'token', value:'token'},
    <SelectItem>{label:'code', value:'code'}
  ];

  constructor(){
    this.response_type = this.responseTypes[0].value;
  }

 onClickTestAuthrorizationWithoutResourceOwnerBasicAuthentication(event:Event) {

    let url:string = `${OAUTH2_SERVER}/oauth2/authorize?client_id=${this.client_id}&response_type=${this.response_type}`;
    if (this.redirect_uri) {
      url = url + `&redirect_uri=${this.redirect_uri}`
    }
    if (this.scope) {
      url = url + `&scope=${this.scope}`
    }
    if (this.state) {
      url = url + `&state=${this.state}`
    }

    window.location.href=url;
 }
}
