# encoding: utf-8

# Install
#    this is a ruby program, so install ruby first
#    after ruby installed, install needed gems
#    gem install nokogiri active_support
#
# Usage
#    ruby android_demos_downloader.rb

require 'nokogiri'
require 'open-uri'
require 'active_support'
require 'pathname'
require "fileutils"

class DownLoader

  def start
    @file_list = {}
    @stack = []
    # @images = {}
    parse_index_page
    current_path = Pathname.new(File.dirname(__FILE__)).realpath.to_s + '/'
    save_codes(@file_list, current_path)
  end

  def parse_index_page
    file_name = 'file_list_json'
    if File.exists?(file_name)
      puts "read file list from file #{file_name}"
      json = ''
      File.open(file_name) do |file|
        file.each do |line|
          json << line
        end
      end
      @file_list = ActiveSupport::JSON.decode(json)
    else
      doc = Nokogiri::HTML(open('http://developer.android.com/intl/zh-cn/samples/index.html'))
      doc.css('#nav > .nav-section').each do |section|
        parse_file_list(section, @file_list)
      end
      json = ActiveSupport::JSON.encode(@file_list)
      file = File.new(file_name, 'w')
      file.puts json
      file.close
      puts "saved file list to file #{file_name}"
    end
  end

  def parse_file_list(section, list)
    links = section.css('> .nav-section-header > a')
    if links && links.length > 0
      attr = links[0].attr('title')
      if attr
        title = attr.class.to_s == 'String' ? attr : attr.value
        list[title] = {}
        index = 0
        section.children.css('> li').each do |li|
          index += 1
          parse_file_list(li, list[title])
        end
      end
    else
      link = section.css('> a')
      if link.length > 0
        url = link.attr('href').value
        file_name = link.attr('title').value
        if is_image?(file_name)
          arr = url.split('/')
          arr[-1] = file_name
          url = arr.join('/')
        end
        list[file_name] = url
      end
    end
  end

  def save_codes(file_list, current_path)
    file_list.each do |name, value|
      if value.class.to_s == 'String'
        unless File.exists? "#{current_path}#{name}"
          code = parse_code_page(name, value)
          puts "save: #{current_path}#{name}"
          save_code(current_path, name, code)
        end
      else
        dir_path = current_path + "#{name.gsub(/\./, '/')}/"
        save_codes(file_list[name], dir_path)
      end
    end
  end

  def parse_code_page(name, url)
    code = ''
    if is_image?(name)
      code = "http://developer.android.com#{url.start_with?('/') ? '' : '/'}#{url}"
    else
      puts "\nparse #{url}"
      t1 = Time.now
      page = open("http://developer.android.com#{url}")
      t2 = Time.now
      puts "download takes #{t2 - t1}s"
      doc = Nokogiri::HTML(page)
      wrappers = doc.css('#codesample-wrapper')
      if wrappers && wrappers[0]
        arr = []
        wrappers[0].content.split("\n").each do |line|
          unless /^\d+$/.match(line.strip) || line.strip == ''
            arr << line
          end
        end
        code = arr.join("\n")
      end
      t3 = Time.now
      puts "parse page takes #{t3 - t2}s"
      puts "total #{t3 - t1}s"
    end

    code
  end

  def save_code(path, name, code)
    mkdir(path)
    if is_image?(name)
      arr = code.split('/')
      full_name = "#{arr[-2]}/#{arr[-1]}"
      # if @images.include?(full_name)
      #   FileUtils.copy(@images[full_name], "#{path}#{name}")
      # else
      data=open(code) { |f| f.read }
      open("#{path}#{name}", "wb") { |f| f.write(data) }
      # @images[full_name] = "#{path}#{name}"
      # end
    else
      unless File.exists? "#{path}#{name}"
        file = File.new("#{path}#{name}", 'w')
        file.puts(code)
        file.close
      end
    end
  end

  def mkdir(path)
    current_path = Pathname.new(File.dirname(__FILE__)).realpath.to_s
    sub_path = path.gsub(current_path, '')
    dir_path = ''
    sub_path.split('/').each do |dir_name|
      if dir_name != ''
        dir_path << dir_name
        Dir.mkdir dir_path unless File.exists? dir_path
        dir_path << '/'
      end
    end
  end


  def is_image?(url)
    ['png', 'jpg', 'gif'].include?(url.split('.')[-1])
  end


end

downloader = DownLoader.new
downloader.start