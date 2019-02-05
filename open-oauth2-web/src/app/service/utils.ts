

export class Utils {
  static reduceToHtmlList(messages:string[]):string {
    if (messages) {
      let msg: string = messages.reduce((acc: string, curr: string) => {
        acc = acc + `<li>${curr}</li>`;
        return acc;
      }, '');
      return `<ul>${msg}</ul>`
    }
    return '';
  }
}
