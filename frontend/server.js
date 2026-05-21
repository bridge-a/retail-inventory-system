const http = require("http");
const fs = require("fs");
const path = require("path");

const rootDir = __dirname;
const port = Number(process.env.PORT || 5173);

const contentTypes = {
    ".html": "text/html; charset=utf-8",
    ".css": "text/css; charset=utf-8",
    ".js": "text/javascript; charset=utf-8",
    ".json": "application/json; charset=utf-8",
    ".svg": "image/svg+xml; charset=utf-8",
    ".png": "image/png",
    ".jpg": "image/jpeg",
    ".jpeg": "image/jpeg",
    ".ico": "image/x-icon"
};

function resolveFilePath(requestUrl) {
    const url = new URL(requestUrl, `http://localhost:${port}`);
    const decodedPath = decodeURIComponent(url.pathname);
    const normalizedPath = path.normalize(decodedPath).replace(/^(\.\.[/\\])+/, "");
    const filePath = path.join(rootDir, normalizedPath === path.sep ? "index.html" : normalizedPath);

    if (!filePath.startsWith(rootDir)) {
        return null;
    }

    return filePath;
}

const server = http.createServer((req, res) => {
    const filePath = resolveFilePath(req.url);
    if (!filePath) {
        res.writeHead(403, { "Content-Type": "text/plain; charset=utf-8" });
        res.end("Forbidden");
        return;
    }

    fs.stat(filePath, (statError, stat) => {
        if (statError || !stat.isFile()) {
            res.writeHead(404, { "Content-Type": "text/plain; charset=utf-8" });
            res.end("Not Found");
            return;
        }

        const ext = path.extname(filePath).toLowerCase();
        res.writeHead(200, {
            "Content-Type": contentTypes[ext] || "application/octet-stream",
            "Cache-Control": "no-store"
        });

        fs.createReadStream(filePath).pipe(res);
    });
});

server.listen(port, "127.0.0.1", () => {
    console.log(`Frontend server running at http://localhost:${port}`);
});
