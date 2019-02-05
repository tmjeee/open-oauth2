import {Component} from "@angular/core";
import {HttpClient, HttpResponse} from "@angular/common/http";
import {map} from "rxjs/operators";


@Component({
  templateUrl:'./not-found.page.html',
  styleUrls:['./not-found.page.scss']
})
export class NotFoundPage {

  constructor(private httpClient:HttpClient) {
  }

  onButtonClicked(event:Event) {
    this.httpClient
      .get("http://localhost:8081/testing/redirect", {
        observe:"response",
        responseType: "text",
      })
      .pipe(
        map((r:HttpResponse<any>)=>{
          console.log(r);
        })
      )
      .subscribe();

  }

}
