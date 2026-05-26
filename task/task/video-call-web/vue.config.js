module.exports = {
  devServer: {
    port: 3000,
    proxy: {
      '/ws': {
        target: 'ws://localhost:8080',
        ws: true
      }
    }
  }
}
