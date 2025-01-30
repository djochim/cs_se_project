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
    tls = {
      source = "hashicorp/tls"
      version = "4.0.6"
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

provider "tls" {
  # Configuration options
}

# ECDSA key with P384 elliptic curve
resource "tls_private_key" "ecdsa-p384-example" {
  algorithm   = "ECDSA"
  ecdsa_curve = "P384"
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

resource "cloudflare_zone_setting" "tls1_3" {
  zone_id = var.cloudflare_zone_id
  setting_id = "tls_1_3"
  value = "on"
}

resource "cloudflare_zone_setting" "min_tls_version" {
  zone_id = var.cloudflare_zone_id
  setting_id = "min_tls_version"
  value = "1.2"
}

resource "cloudflare_origin_ca_certificate" "origin-cert" {
  csr                  = tls_cert_request.api-cert-request.cert_request_pem
  hostnames            = ["*.jochim.dev", "jochim.dev"]
  request_type         = "origin-rsa"
  requested_validity   = 365
}

# resource "cloudflare_zone_setting" "ssl" {
#   zone_id = var.cloudflare_zone_id
#   setting_id = "ssl"
#   value = "strict"
# }

resource "cloudflare_zone_setting" "https_rewrites" {
  zone_id = var.cloudflare_zone_id
  setting_id = "automatic_https_rewrites"
  value = "on"
}

resource "cloudflare_zone_setting" "waf" {
  zone_id = var.cloudflare_zone_id
  setting_id = "waf"
  value = "on"
}