// https://deno.land/x/nativerouter@1.0.0/mod.ts?source

type CallbackHandler = (
  request: Request,
  params: Record<string, string>,
) => Promise<Response>;

const METHODS: Record<string, string> = {
  GET: "GET",
  HEAD: "HEAD",
  POST: "POST",
  PUT: "PUT",
  DELETE: "DELETE",
  OPTIONS: "OPTIONS",
  TRACE: "TRACE",
  PATCH: "PATCH",
};

export class Router {
  private routes: Record<string, Array<any>> = {};

  constructor() {
    for (const m in METHODS) {
      this.routes[METHODS[m]] = [];
    }
  }

  private add(method: string, pathname: string, handler: CallbackHandler) {
    this.routes[method].push({
      pattern: new URLPattern({ pathname }),
      handler,
    });
  }

  get(pathname: string, handler: CallbackHandler) {
    this.add(METHODS.GET, pathname, handler);
  }

  head(pathname: string, handler: CallbackHandler) {
    this.add(METHODS.HEAD, pathname, handler);
  }

  post(pathname: string, handler: CallbackHandler) {
    this.add(METHODS.POST, pathname, handler);
  }

  put(pathname: string, handler: CallbackHandler) {
    this.add(METHODS.PUT, pathname, handler);
  }

  delete(pathname: string, handler: CallbackHandler) {
    this.add(METHODS.DELETE, pathname, handler);
  }

  options(pathname: string, handler: CallbackHandler) {
    this.add(METHODS.OPTIONS, pathname, handler);
  }

  trace(pathname: string, handler: CallbackHandler) {
    this.add(METHODS.TRACE, pathname, handler);
  }

  patch(pathname: string, handler: CallbackHandler) {
    this.add(METHODS.PATCH, pathname, handler);
  }

  async route(req: Request): Promise<Response> {
    console.log(`[mod.ts#route] Request: ${req.method} ${req.url}`)
    for (const rt of this.routes[req.method]) {
      if (rt.pattern.test(req.url)) {
        const params = rt.pattern.exec(req.url).pathname.groups;
        try {
          return await rt["handler"](req, params);
        } catch (err) {
          console.trace(`[native-router/mod.ts] responding with HTTP Status: 500; ${err}` )
          return new Response(null, { status: 500 });
        }
      }
    }
    console.log('[native-router/mod.ts] responding HTTP Status: 404');
    return new Response(null, { status: 404 });
  }
}
