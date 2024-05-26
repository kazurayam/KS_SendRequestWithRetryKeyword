// https://medium.com/deno-the-complete-reference/native-router-in-deno-16595970daae
import { Router } from "./native-router/mod.ts";
import { serve } from "https://deno.land/std/http/mod.ts";
import { randomNumber } from "https://deno.land/x/random_number/mod.ts";
import { modulo } from "https://deno.land/x/modulo/mod.ts";


const router = new Router();

router.get("/hello", async (req: Request, _params: Record<string, string>) => {
    let text = 'Hello';
    const u = new URL(req.url);
    const name = u.searchParams.get('name');
    if (name !== null) {
        text = `Hello, ${name}!`
    }
    return new Response(text,
                        { headers:{"Content-Type": "text/plain; charset=utf-8"}});
});

router.post("/hello", async (req: Request, _params: Record<string, string>) => {
    let text = 'Hello';
    const formData = await req.formData();
    const name = formData.get('name');
    if (name !== '') {
        text = `Hello, ${formData.get('name')}`;
    }
    return new Response(text,
                        { headers: {"Content-Type": "text/plain; charset=utf-8"}});
});

router.get("/hello/:name", async ( _req: Request, params: Record<string, string>) => {
    return new Response(`Hello, ${params.name}!`,
                        { headers: {"content-type": "text/plain; charset=utf-8"}});
});

router.get("/", async ( _req : Request, _params: Record<string, string>) => {
    const html = await Deno.readTextFile("./index.html");
    return new Response(html, { headers: {"content-type": "text/html; charset=utf-8"}});
});

router.get("/:filename.html", async ( _ : Request, params: Record<string, string>) => {
    const html = await Deno.readTextFile(`${params.filename}.html`);
    return new Response(html, { headers: {"content-type": "text/html; charset=utf-8"}});
});

router.get("/scripts/:filename.js", async ( _ : Request, params: Record<string, string>) => {
    const html = await Deno.readTextFile(`scripts/${params.filename}.js`);
    return new Response(html, { headers: {"content-type": "application/javascript; charset=utf-8"}});
});

router.get("/:filename.json", async (_req: Request, params: Record<string, string>) => {
    const html = await Deno.readTextFile(`${params.filename}.json`);
    return new Response(html, { headers: {"content-type": "application/json; charset=utf-8"}});
});

/* an naughty URL to test the case of
 *     https://forum.katalon.com/t/how-to-retry-at-verification-tab-on-api-objet/131571/5
 *
 * In most cases, this url returns an JSON with HTTP STATUS=200.
 * But occasionaly it returns an HTML with HTTP STATUS=500.
 *
 */
router.get("/naughty", async (_req: Request, params: Record<string, string>) => {
  const r = randomNumber({ min: 1, max: 100 });
  if (modulo(r, 3) === 0) {
    const html = await Deno.readTextFile(`error.html`);
    return new Response(html, { status: 500, headers: {"content-type": "text/html; charset=utf-8"}});
  } else {
    const html = await Deno.readTextFile(`book.json`);
    return new Response(html, { headers: {"content-type": "application/json; charset=utf-8"}});
  }
});



async function reqHandler(req: Request): Promise<Response> {
    console.log(`\n[serve.ts#reqHandler] Request:  ${req.method} ${req.url}`);
    return await router.route(req);
}
serve(reqHandler, { port: 3000 });
