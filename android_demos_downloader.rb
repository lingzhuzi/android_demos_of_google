# encoding: utf-8

require 'nokogiri'
require 'open-uri'
require 'active_support'
require 'pathname'

class DownLoader

  def run
    @file_list = {}
    @stack = []
    @images = {}
    parse_index_page
    current_path = Pathname.new(File.dirname(__FILE__)).realpath
    save_codes(@file_list, current_path)
  end

  def parse_index_page
    doc = Nokogiri::HTML(open('http://developer.android.com/intl/zh-cn/samples/index.html'))
    doc.css('#nav > .nav-section').each do |section|
      parse_file_list(section, @file_list)
    end
    # json = ActiveSupport::JSON.encode(@file_list)
    # file = File.new('file_list_json', 'w')
    # file.puts json
    # file.close

  end

  def parse_file_list(section, list)
    links = section.css('> .nav-section-header > a')
    if links && links.length > 0
      attr = links[0].attr('title')
      if attr
        title = attr.class.to_s == 'String' ? attr : attr.value
        list[title] = {}
        section.children.css('li').each do |li|
          parse_file_list(li, list[title])
        end
      end
    else
      link = section.css('> a')
      if link.length > 0
        url = link.attr('href').value
        file_name = link.attr('title').value
        list[file_name] = url
      end
    end
  end

  def save_codes(file_list, current_path)
    file_list.each do |name, value|
      if value.class.to_s == 'String'
        code = parse_code_page(name, value)
        save_code(current_path, name, code)
      else
        save_codes(file_list[name], "#{current_path}/name")
      end
    end
  end

  def parse_code_page(name, url)
    code = ''
    if is_image?(name)
      code = "http://developer.android.com#{url.start_with?('/') ? '' : '/'}#{url}"
    else
      puts "parse #{url}"
      doc = Nokogiri::HTML(open("http://developer.android.com#{url}"))
      wrappers = doc.css('#codesample-wrapper')
      if wrappers && wrappers[0]
        arr = []
        wrappers[0].content.split("\n").each do |line|
          unless /\d/.match(line.strip)
            arr << line
          end
          code = arr.join("\n")
          puts code
        end

      end
    end
  end

  def save_code(path, name, code)

  end


  def is_image?(url)
    ['png', 'jpg', 'gif'].include?(url.split('.')[-1])
  end

end

downloader = DownLoader.new
downloader.run