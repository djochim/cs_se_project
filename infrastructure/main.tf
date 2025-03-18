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
      source  = "cloudflare/cloudflare"
      version = "5.0.0-rc1"
    }
    tls = {
      source  = "hashicorp/tls"
      version = "4.0.6"
    }
  }
}

provider "tls" {
  # Configuration options
}

# ECDSA key for ssh access
resource "tls_private_key" "pipeline_ssh" {
  algorithm   = "ECDSA"
  ecdsa_curve = "P521"
}

# ECDSA key with P384 elliptic curve
resource "tls_private_key" "api_cert_private_key" {
  algorithm   = "ECDSA"
  ecdsa_curve = "P384"
}

resource "tls_cert_request" "api_cert_request" {
  private_key_pem = tls_private_key.api_cert_private_key.private_key_pem
}

provider "cloudflare" {
  api_token = var.cloudflare_token
}

resource "cloudflare_origin_ca_certificate" "origin_cert" {
  csr                = tls_cert_request.api_cert_request.cert_request_pem
  hostnames          = ["*.jochim.dev", "jochim.dev"]
  request_type       = "origin-rsa"
  requested_validity = 365
}

provider "ct" {
  # Configuration options
}

data "ct_config" "flatcar_ignition" {
  content = data.template_file.flatcar_cl_config.rendered
}

data "template_file" "flatcar_cl_config" {
  template = file("${path.module}/flatcar-config.yaml.tmpl")
  vars = {
    appname           = var.appname
    developer_ssh     = var.developer_ssh
    pipeline_ssh      = tls_private_key.pipeline_ssh.public_key_openssh
    private_key       = base64encode(tls_private_key.api_cert_private_key.private_key_pem)
    certificate       = base64encode(cloudflare_origin_ca_certificate.origin_cert.certificate)
    origin_ca         = base64encode(file("${path.module}/cf-origin-ca.pem"))
    nginx_conf_base64 = base64encode(file("${path.module}/nginx.conf"))
    github_email      = var.github_email
    github_auth       = var.github_token
  }
}

provider "hcloud" {
  # Configuration options
  token = var.hcloud_token
}

# Create a new server running debian
resource "hcloud_server" "aeon_server" {
  name        = var.server_name
  image       = "212121145"
  server_type = var.server_type
  location    = var.server_location
  user_data   = data.ct_config.flatcar_ignition.rendered
  public_net {
    ipv4_enabled = true
    ipv6_enabled = true
  }
}

resource "cloudflare_dns_record" "main_dns" {
  zone_id = var.cloudflare_zone_id
  name    = var.domain
  content = hcloud_server.aeon_server.ipv4_address
  ttl     = 1
  type    = "A"
  proxied = true
}

resource "cloudflare_dns_record" "aeon_dns" {
  zone_id = var.cloudflare_zone_id
  name    = var.aeon_subdomain
  content = hcloud_server.aeon_server.ipv4_address
  ttl     = 1
  type    = "A"
  proxied = true
}

resource "cloudflare_zone_setting" "tls1_3" {
  zone_id    = var.cloudflare_zone_id
  setting_id = "tls_1_3"
  value      = "on"
}

resource "cloudflare_zone_setting" "min_tls_version" {
  zone_id    = var.cloudflare_zone_id
  setting_id = "min_tls_version"
  value      = "1.2"
}

resource "cloudflare_zone_setting" "ssl" {
  zone_id    = var.cloudflare_zone_id
  setting_id = "ssl"
  value      = "strict"
}

resource "cloudflare_zone_setting" "https_rewrites" {
  zone_id    = var.cloudflare_zone_id
  setting_id = "automatic_https_rewrites"
  value      = "on"
}