terraform {
  required_providers {
    hcloud = {
      source  = "hetznercloud/hcloud"
      version = "1.49.1"
    }
    ct = {
      source  = "poseidon/ct"
      version = "0.13.0"
    }
    cloudflare = {
      source = "cloudflare/cloudflare"
      version = "5.0.0-rc1"
    }
  }
}

provider "ct" {
  # Configuration options
}

data "ct_config" "flatcar-ignition" {
  content = data.template_file.flatcar-cl-config.rendered
}

data "template_file" "flatcar-cl-config" {
  template = file("${path.module}/flatcar-config.yaml.tmpl")
  vars     = { appname = var.appname }
}

provider "hcloud" {
  # Configuration options
  token = var.hcloud_token
}

# Create a new server running debian
resource "hcloud_server" "aeon-server" {
  name        = var.server_name
  image       = "212121145"
  server_type = var.server_type
  location    = var.server_location
  user_data   = data.ct_config.flatcar-ignition.rendered
  public_net {
    ipv4_enabled = true
    ipv6_enabled = true
  }
}

provider "cloudflare" {
  api_token = var.cloudflare_token
}

resource "cloudflare_dns_record" "main_dns" {
  zone_id = var.cloudflare_zone_id
  name    = var.domain
  content  = var.ip
  ttl = 1
  type    = "A"
  proxied = true
}